package com.selfgrowthfund.sgf.ui.shareholders

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.selfgrowthfund.sgf.R
import com.selfgrowthfund.sgf.databinding.FragmentShareholderBinding
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class ShareholderFragment : Fragment(R.layout.fragment_shareholder) {

    private var _binding: FragmentShareholderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShareholderViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShareholderBinding.bind(view)

        observeSubmissionResult()

        binding.submitButton.setOnClickListener {
            viewModel.submitShareholder(
                fullName = binding.fullNameInput.text.toString(),
                mobileNumber = binding.mobileInput.text.toString(),
                address = binding.addressInput.text.toString(),
                shareBalanceInput = binding.shareBalanceInput.text.toString(),
                joinDate = Date() // Replace with actual date picker value
            )
        }
    }

    private fun observeSubmissionResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.submissionResult.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            Toast.makeText(requireContext(), "Shareholder added successfully!", Toast.LENGTH_SHORT).show()
                            viewModel.clearSubmissionResult()
                        }
                        is Result.Error -> {
                            Toast.makeText(requireContext(), "Error: ${result.exception.message}", Toast.LENGTH_LONG).show()
                            viewModel.clearSubmissionResult()
                        }
                        Result.Loading -> {
                            // Optional: show loading spinner
                        }
                        null -> {
                            // No-op
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}