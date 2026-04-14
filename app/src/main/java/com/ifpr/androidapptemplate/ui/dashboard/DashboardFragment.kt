package com.ifpr.androidapptemplate.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var enderecoEditText: EditText
    private lateinit var nomeEditText: EditText
    private lateinit var datanascimentoEditText: EditText
    private lateinit var itemImageView: ImageView
    private lateinit var salvarButton: Button
    private lateinit var selectImageButton: Button

    private var imageUri: Uri? = null

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        itemImageView = view.findViewById(R.id.image_item)
        salvarButton = view.findViewById(R.id.salvarItemButton)
        selectImageButton = view.findViewById(R.id.button_select_image)
        enderecoEditText = view.findViewById(R.id.enderecoItemEditText)
        nomeEditText = view.findViewById(R.id.nomeItemEditText)
        datanascimentoEditText = view.findViewById(R.id.datanascimentoItemEditText)

        auth = FirebaseAuth.getInstance()

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        salvarButton.setOnClickListener {
            salvarItem()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun salvarItem() {
        val nome = nomeEditText.text.toString().trim()
        val endereco = enderecoEditText.text.toString().trim()
        val datanascimento = datanascimentoEditText.text.toString().trim()

        if (nome.isEmpty() || endereco.isEmpty() || datanascimento.isEmpty() || imageUri == null) {
            Toast.makeText(
                context,
                "Por favor, preencha todos os campos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        uploadImageToFirestore()
    }

    private fun uploadImageToFirestore() {
        if (imageUri != null) {
            val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

                val nome = nomeEditText.text.toString().trim()
                val endereco = enderecoEditText.text.toString().trim()
                val datanascimento = datanascimentoEditText.text.toString().trim()

                val item = Item(
                    nome,
                    endereco,
                    base64Image,
                    null,
                    datanascimento
                )

                saveItemIntoDatabase(item)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == Activity.RESULT_OK &&
            data != null &&
            data.data != null
        ) {
            imageUri = data.data

            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(itemImageView)
        }
    }

    private fun saveItemIntoDatabase(item: Item) {
        databaseReference = FirebaseDatabase.getInstance().getReference("itens")

        val itemId = databaseReference.push().key

        if (itemId != null) {
            databaseReference
                .child(auth.uid.toString())
                .child(itemId)
                .setValue(item)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Item cadastrado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    nomeEditText.text.clear()
                    enderecoEditText.text.clear()
                    datanascimentoEditText.text.clear()
                    itemImageView.setImageResource(0)
                    imageUri = null
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Falha ao cadastrar o item",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(
                context,
                "Erro ao gerar ID do item",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}