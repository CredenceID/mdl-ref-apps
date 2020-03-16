/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.security.identity;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedList;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.builder.ArrayBuilder;
import co.nstant.in.cbor.builder.MapBuilder;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnicodeString;

class SoftwareWritableIdentityCredential extends WritableIdentityCredential {

    private static final String TAG = "SoftwareWritableIdentityCredential";

    private KeyPair mKeyPair = null;
    private Collection<X509Certificate> mCertificates = null;
    private String mDocType;
    private String mCredentialName;
    private Context mContext;

    SoftwareWritableIdentityCredential(Context context,
            @NonNull String credentialName,
            @NonNull String docType) throws AlreadyPersonalizedException {
        mContext = context;
        mDocType = docType;
        mCredentialName = credentialName;
        if (CredentialData.credentialAlreadyExists(context, credentialName)) {
            throw new AlreadyPersonalizedException("Credential with given name already exists");
        }
    }

    /**
     * Generates CredentialKey.
     *
     * If called a second time on the same object, does nothing and returns null.
     *
     * @param challenge The attestation challenge.
     * @return Attestation mCertificate chain or null if called a second time.
     * @throws AlreadyPersonalizedException     if this credential has already been personalized.
     * @throws CipherSuiteNotSupportedException if the cipher suite is not supported
     * @throws IdentityCredentialException      if unable to communicate with secure hardware.
     */
    private Collection<X509Certificate> ensureCredentialKey(byte[] challenge) {

        if (mKeyPair != null) {
            return null;
        }

        String aliasForCredential = CredentialData.getAliasFromCredentialName(mCredentialName);

        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (ks.containsAlias(aliasForCredential)) {
                ks.deleteEntry(aliasForCredential);
            }

            // TODO: We most likely want to constrain the life of CredentialKey (through
            // setKeyValidityStart() and setKeyValidityEnd()) so it's limited to e.g. 5 years
            // or how long the credential might be valid. For US driving licenses it's typically
            // up to 5 years, where it expires on your birth day).
            //
            // This is likely something the issuer would want to specify.

            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    aliasForCredential,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512);

            // Attestation is only available in Nougat and onwards.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (challenge == null) {
                    challenge = new byte[0];
                }
                builder.setAttestationChallenge(challenge);
            }
            kpg.initialize(builder.build());
            mKeyPair = kpg.generateKeyPair();

            Certificate[] certificates = ks.getCertificateChain(aliasForCredential);
            mCertificates = new LinkedList<>();
            for (Certificate certificate : certificates) {
                mCertificates.add((X509Certificate) certificate);
            }
        } catch (InvalidAlgorithmParameterException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | CertificateException
                | KeyStoreException
                | IOException e) {
            throw new RuntimeException("Error creating CredentialKey", e);
        }
        return mCertificates;
    }

    @Override
    public @NonNull Collection<X509Certificate> getCredentialKeyCertificateChain(
            @NonNull byte[] challenge) {
        Collection<X509Certificate> certificates = ensureCredentialKey(challenge);
        if (certificates == null) {
            throw new RuntimeException(
                    "getCredentialKeyCertificateChain() must be called before personalize()");
        }
        return certificates;
    }

    // Returns COSE_Sign1 with payload set to ProofOfProvisioning
    static byte[] buildProofOfProvisioningWithSignature(String docType,
            Collection<AccessControlProfile> accessControlProfiles,
            Collection<EntryNamespace> entryNamespaces,
            PrivateKey key) {

        CborBuilder accessControlProfileBuilder = new CborBuilder();
        ArrayBuilder<CborBuilder> arrayBuilder = accessControlProfileBuilder.addArray();
        for (AccessControlProfile profile : accessControlProfiles) {
            arrayBuilder.add(Util.accessControlProfileToCbor(profile));
        }

        CborBuilder dataBuilder = new CborBuilder();
        MapBuilder<CborBuilder> dataMapBuilder = dataBuilder.addMap();
        for (EntryNamespace entryNamespace : entryNamespaces) {
            dataMapBuilder.put(
                    new UnicodeString(entryNamespace.getNamespaceName()),
                    Util.entryNamespaceToCbor(entryNamespace));
        }

        CborBuilder signedDataBuilder = new CborBuilder();
        ArrayBuilder<CborBuilder> signedDataArrayBuilder = signedDataBuilder.addArray()
                .add("ProofOfProvisioning")
                .add(docType)
                .add(accessControlProfileBuilder.build().get(0))
                .add(dataBuilder.build().get(0))
                .add(false);

        byte[] signatureBytes;
        try {
            ByteArrayOutputStream dtsBaos = new ByteArrayOutputStream();
            CborEncoder dtsEncoder = new CborEncoder(dtsBaos);
            dtsEncoder.encode(signedDataBuilder.build().get(0));
            byte[] dataToSign = dtsBaos.toByteArray();

            signatureBytes = Util.coseSign1Sign(key,
                    dataToSign,
                    null,
                    null);
        } catch (NoSuchAlgorithmException
                | InvalidKeyException
                | CertificateEncodingException
                | CborException e) {
            throw new RuntimeException("Error building ProofOfProvisioning", e);
        }
        return signatureBytes;
    }

    @NonNull
    @Override
    public byte[] personalize(@NonNull Collection<AccessControlProfile> accessControlProfiles,
            @NonNull Collection<EntryNamespace> entryNamespaces) {

        ensureCredentialKey(null);

        byte[] encodedBytes = buildProofOfProvisioningWithSignature(mDocType,
                accessControlProfiles,
                entryNamespaces,
                mKeyPair.getPrivate());

        CredentialData data = CredentialData.createCredentialData(
                mContext,
                mDocType,
                mCredentialName,
                CredentialData.getAliasFromCredentialName(mCredentialName),
                mCertificates,
                accessControlProfiles,
                entryNamespaces);

        return encodedBytes;
    }

}