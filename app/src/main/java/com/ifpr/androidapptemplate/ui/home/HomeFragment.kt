package com.ifpr.androidapptemplate.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import android.util.Base64
import android.graphics.BitmapFactory
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val itemContainer = view.findViewById<LinearLayout>(R.id.itemContainer)
        carregarItensMarketplace(itemContainer)

        return view
    }

    fun carregarItensMarketplace(container: LinearLayout) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("itens")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()

                for (userSnapshot in snapshot.children) {
                    for (itemSnapshot in userSnapshot.children) {
                        val item = itemSnapshot.getValue(Item::class.java) ?: continue

                        val itemView = LayoutInflater.from(container.context)
                            .inflate(R.layout.item_template, container, false)

                        val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                        val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)
                        val dataNascimentoView = itemView.findViewById<TextView>(R.id.item_datanascimento)
                        val nomeView = itemView.findViewById<TextView>(R.id.item_nome)


                        enderecoView.text = "Endereço: ${item.endereco ?: "Não informado"}"
                        dataNascimentoView.text = "Data: ${item.datanascimento ?: "Não informado"}"
                        nomeView.text = "Nome: ${item.nome ?: "Não informado"}"


                        if (!item.imageUrl.isNullOrEmpty()) {
                            Glide.with(container.context)
                                .load(item.imageUrl)
                                .into(imageView)
                        } else if (!item.base64Image.isNullOrEmpty()) {
                            try {
                                val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                imageView.setImageBitmap(bitmap)
                            } catch (_: Exception) {
                            }
                        }

                        container.addView(itemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    container.context,
                    "Erro ao carregar dados",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}