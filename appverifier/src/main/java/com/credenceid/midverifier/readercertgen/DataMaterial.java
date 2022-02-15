package com.credenceid.midverifier.readercertgen;

import java.util.Optional;

public interface DataMaterial {
	String subjectDN();

	String issuerDN();

	Optional<String> issuerAlternativeName();
}

