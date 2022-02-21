package com.android.mdl.app.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.mdl.app.R
import com.android.mdl.app.databinding.FragmentModifyDetailsBinding
import com.android.mdl.app.document.DocumentManager
import com.android.mdl.app.model.DLPersonalDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.backgroundColor


/**
 * create an instance of this fragment.
 */
class ModifyDetailsFragment : Fragment() {

    private var _binding: FragmentModifyDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var input = DLPersonalDetails()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentModifyDetailsBinding.inflate(inflater)
        binding.fragment = this
        return binding.root
    }

    fun onSelectImage() {
        input.licenceDB = R.drawable.img_licence_2
    }

    fun onSelectPicture1() {
        binding.radio1.backgroundColor = Color.BLACK
        binding.radio2.backgroundColor = Color.WHITE
        binding.radio3.backgroundColor = Color.WHITE
        input.licenceDB = R.drawable.img_licence_2
    }

    fun onSelectPicture2() {
        binding.radio1.backgroundColor = Color.WHITE
        binding.radio2.backgroundColor = Color.BLACK
        binding.radio3.backgroundColor = Color.WHITE
        input.licenceDB = R.drawable.img_erika_portrait
    }

    fun onSelectPicture3() {
        binding.radio1.backgroundColor = Color.WHITE
        binding.radio2.backgroundColor = Color.WHITE
        binding.radio3.backgroundColor = Color.BLACK
        input.licenceDB = R.drawable.img_licence_3
    }

    fun onUpdateDocument() {
        val firstName = binding.etInputFirstName.text
        val lastName = binding.etInputLastName.text
        val dob = binding.etInputDob.text
        input.name = firstName.toString()
        input.lastName = lastName.toString()
        input.dob = dob.toString()
        binding.progressBar.isVisible = true
        val value = CoroutineScope(Dispatchers.IO).async {
            DocumentManager.getInstance(requireContext()).updateCreatedDocuments(input)
        }

        CoroutineScope(Dispatchers.Main).launch {
            value.await()
            binding.progressBar.isVisible = false
            findNavController().navigate(
                ModifyDetailsFragmentDirections.actionModifyDetailsFragmentToSelectDocumentFragment()
            )

        }
    }

    fun onCancel() {
        findNavController().navigate(
            ModifyDetailsFragmentDirections.actionModifyDetailsFragmentToSelectDocumentFragment()
        )
    }

}