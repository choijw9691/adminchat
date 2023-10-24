package com.project.adminchat.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.project.adminchat.databinding.FragmentConfirmationDialogBinding

class ConfirmationDialog : DialogFragment() {
    private var _binding: FragmentConfirmationDialogBinding? = null
    private val binding get() = _binding!!
    private var dialogTitle: String? = null

    var confirmAction: (() -> Unit)? = null
    var cancelAction: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmationDialogBinding.inflate(inflater, container, false)

        binding.confirmDialog.setOnClickListener {
            confirmAction?.invoke()
            dismiss()
        }

        binding.cancelDialog.setOnClickListener {
            cancelAction?.invoke()
            dismiss()
        }

        dialogTitle?.let { title ->
            binding.titleDialog.text = title
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            600
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun show(
        manager: FragmentManager,
        title: String,
        confirmAction: (() -> Unit)?,
        cancelAction: (() -> Unit)?
    ) {
        this.confirmAction = confirmAction
        this.cancelAction = cancelAction
        this.dialogTitle = title
        super.show(manager, tag)
    }
}
