package org.oppia.android.app.profile

import android.app.Dialog
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.utility.TextInputEditTextHelper.Companion.onTextChanged
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ResetPinDialogBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [ResetPinDialogFragment]. */
@FragmentScope
class ResetPinDialogFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<ResetPinViewModel>
) {
  private val resetViewModel by lazy {
    getResetPinViewModel()
  }

  fun handleOnCreateDialog(
    routeDialogInterface: ProfileRouteDialogInterface,
    profileId: Int,
    name: String
  ): Dialog {
    val binding: ResetPinDialogBinding = DataBindingUtil.inflate(
      activity.layoutInflater,
      R.layout.reset_pin_dialog,
      /* parent= */ null,
      /* attachToParent= */ false
    )
    binding.apply {
      lifecycleOwner = fragment
      viewModel = resetViewModel
    }

    binding.inputPinEditText.setText(resetViewModel.inputPin.get().toString())
//    binding.inputPin.addTextChangedListener(object : TextWatcher {
//      override fun onTextChanged(confirmPin: CharSequence?, start: Int, before: Int, count: Int) {
//        confirmPin?.let {
//          resetViewModel.inputPin.set(confirmPin.toString())
//          resetViewModel.errorMessage.set("")
//        }
//      }
//
//      override fun afterTextChanged(confirmPin: Editable?) {}
//      override fun beforeTextChanged(p0: CharSequence?, start: Int, count: Int, after: Int) {}
//    })
    binding.inputPinEditText.onTextChanged { confirmPin ->
      confirmPin?.let {
        resetViewModel.inputPin.set(confirmPin.toString())
        resetViewModel.errorMessage.set("")
      }
    }

    binding.inputPin.hint = activity.resources
      .getString(R.string.admin_settings_enter_user_new_pin, name)

    val dialog = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(R.string.reset_pin_enter)
      .setView(binding.root)
      .setMessage("This PIN wil be $name's new PIN and will be required when signing in.")
      .setPositiveButton(R.string.admin_settings_submit, null)
      .setNegativeButton(R.string.admin_settings_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .create()

    binding.inputPinEditText.setOnEditorActionListener { _, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE ||
        (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER))
      ) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick()
      }
      false
    }

    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val input = binding.inputPinEditText.text.toString()
        if (input.isEmpty()) {
          return@setOnClickListener
        }
        if (input.length == 3) {
          profileManagementController
            .updatePin(ProfileId.newBuilder().setInternalId(profileId).build(), input).toLiveData()
            .observe(
              fragment,
              Observer {
                if (it.isSuccess()) {
                  routeDialogInterface.routeToSuccessDialog()
                }
              }
            )
        } else {
          resetViewModel.errorMessage.set(
            fragment.resources.getString(
              R.string.add_profile_error_pin_length
            )
          )
        }
      }
    }
    return dialog
  }

  private fun getResetPinViewModel(): ResetPinViewModel {
    return viewModelProvider.getForFragment(fragment, ResetPinViewModel::class.java)
  }
}
