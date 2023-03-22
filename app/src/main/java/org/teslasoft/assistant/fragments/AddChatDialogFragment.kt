package org.teslasoft.assistant.fragments

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.teslasoft.assistant.R
import org.teslasoft.assistant.util.Hash
import java.lang.Exception
import java.lang.reflect.Type

class AddChatDialogFragment : DialogFragment() {

    companion object {
        public fun newInstance(name: String) : AddChatDialogFragment {
            val addChatDialogFragment = AddChatDialogFragment()

            val args = Bundle()
            args.putString("name", name)

            addChatDialogFragment.arguments = args

            return addChatDialogFragment
        }
    }

    private var builder: AlertDialog.Builder? = null

    private var context: Context? = null

    private var nameInput: EditText? = null

    private var listener: StateChangesListener? = null

    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext())

        val view: View = this.layoutInflater.inflate(R.layout.add_dialog, null)

        nameInput = view.findViewById(R.id.field_name)

        val dialogTitle: TextView = view.findViewById(R.id.dialog_title)

        if (requireArguments().getString("name") != "") {
            dialogTitle.text = "Edit chat"

            nameInput?.setText(requireArguments().getString("name"))

            builder!!.setView(view)
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> validateForm() }
                    .setNeutralButton("Delete") { _, _ -> confirmDeletion(requireActivity()) }
                    .setNegativeButton("Cancel") { _, _ -> listener!!.onCanceled() }

            isEdit = true

            return builder!!.create()
        } else {
            dialogTitle.text = "New chat"

            builder!!.setView(view)
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> validateForm() }
                    .setNegativeButton("Cancel") { _, _: Int -> listener!!.onCanceled() }

            return builder!!.create()
        }
    }

    private fun validateForm() {
        if (nameInput?.text.toString() == "") {
            listener!!.onError()
        } else {
            val settings: SharedPreferences = requireActivity().getSharedPreferences("chat_list", Context.MODE_PRIVATE)

            val list: ArrayList<HashMap<String, String>>

            val gson = Gson()
            val json = settings.getString("data", null)
            val type: Type = object : TypeToken<ArrayList<HashMap<String, String>?>?>() {}.type

            list = try {
                gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, String>>
            } catch (e: Exception) {
                arrayListOf()
            }

            if (isEdit) {
                for (map: HashMap<String, String> in list) {
                    if (map["id"] == Hash.hash(requireArguments().getString("name").toString())) {
                        var isFound = false
                        for (map2: HashMap<String, String> in list) {
                            if (map2["id"] == Hash.hash(nameInput?.text.toString())) {
                                isFound = true
                                break
                            }
                        }

                        if (!isFound) {
                            map["name"] = nameInput?.text.toString()
                            map["id"] = Hash.hash(nameInput?.text.toString())

                            val editor = settings.edit()
                            val gson2 = Gson()
                            val json2: String = gson2.toJson(list)

                            editor.putString("data", json2)
                            editor.apply()

                            val settings1: SharedPreferences = requireActivity().getSharedPreferences("chat_${Hash.hash(requireArguments().getString("name").toString())}", Context.MODE_PRIVATE)

                            val str = settings1.getString("chat", "")

                            val ed1 = settings1.edit()

                            ed1.clear()

                            ed1.apply()

                            val settings2: SharedPreferences = requireActivity().getSharedPreferences("chat_${Hash.hash(nameInput?.text.toString())}", Context.MODE_PRIVATE)

                            val ed = settings2.edit()

                            ed.putString("chat", str)

                            ed.apply()

                            listener!!.onEdit()
                        } else {
                            listener!!.onDuplicate()
                        }

                        break
                    }
                }
            } else {
                var isFound = false
                for (map: HashMap<String, String> in list) {
                    if (map["id"] == Hash.hash(nameInput?.text.toString())) {
                        isFound = true
                        break
                    }
                }

                if (!isFound) {
                    val map: HashMap<String, String> = HashMap()

                    map["name"] = nameInput?.text.toString()
                    map["id"] = Hash.hash(nameInput?.text.toString())

                    list.add(map)

                    val editor = settings.edit()
                    val gson2 = Gson()
                    val json2: String = gson2.toJson(list)

                    editor.putString("data", json2)
                    editor.apply()

                    listener!!.onEdit()
                } else {
                    listener!!.onDuplicate()
                }
            }
        }
    }

    private fun confirmDeletion(context: Context) {
        MaterialAlertDialogBuilder(requireActivity(), R.style.App_MaterialAlertDialog)
                .setTitle("Confirm deletion")
                .setMessage("This action can not be undone.")
                .setPositiveButton("Delete") { _, _ -> delete(context) }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
    }

    private fun delete(context: Context) {
        val settings: SharedPreferences = context.getSharedPreferences("chat_list", Context.MODE_PRIVATE)

        val list: ArrayList<HashMap<String, String>>

        val gson = Gson()
        val json = settings.getString("data", null)
        val type: Type = object : TypeToken<ArrayList<HashMap<String, String>?>?>() {}.type

        list = try {
            gson.fromJson<Any>(json, type) as ArrayList<HashMap<String, String>>
        } catch (e: Exception) {
            arrayListOf()
        }

        for (map: HashMap<String, String> in list) {
            if (map["name"] == requireArguments().getString("name")) {
                list.remove(map)
                break
            }
        }

        val editor = settings.edit()
        val gson2 = Gson()
        val json2: String = gson2.toJson(list)

        editor.putString("data", json2)
        editor.apply()

        val settings2: SharedPreferences = context.getSharedPreferences("chat_${Hash.hash(requireArguments().getString("name").toString())}", Context.MODE_PRIVATE)

        val ed = settings2.edit()

        ed.clear()

        ed.apply()

        listener!!.onDelete()
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    public interface StateChangesListener {
        public fun onEdit()
        public fun onError()
        public fun onCanceled()
        public fun onDelete()
        public fun onDuplicate()
    }
}