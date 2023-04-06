package com.example.movieretrofit.fragments.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.movieretrofit.Firebase
import com.example.movieretrofit.R
import com.example.movieretrofit.data.Nutrients
import com.example.movieretrofit.databinding.FragmentHomeBinding
import com.example.movieretrofit.model.SharedViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var firebase: Firebase
    private var launcher: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebase = Firebase()
        firebase.loadUser()
        firebase.getNutrientsFromFirebase { setBarChart(it) }

//        launcher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//                if (result.resultCode == AppCompatActivity.RESULT_OK) {
//                    handleNutrientsData(result.data)
//                }
//            }

        onClickDelete()
        updateNutrients()
    }

    private fun onClickDelete() {
        binding.btnDelete.setOnClickListener {
            val query = firebase.dateRef

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val lastChildKey = dataSnapshot.children.lastOrNull()?.key
                    lastChildKey?.let { key ->
                        query.child(key).removeValue()
                    }
                    firebase.getNutrientsFromFirebase { setBarChart(it) }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("item", "onCancelled", databaseError.toException())
                }
            })
        }
    }

    private fun updateNutrients() {
        val viewModel: SharedViewModel by activityViewModels()
        viewModel.data.observe(viewLifecycleOwner) { data ->
            firebase.sendDataToFirebase(data)
            firebase.getNutrientsFromFirebase { setBarChart(it) }
        }
    }


    private fun setBarChart(nutrients: Nutrients) {
        val entries = ArrayList<BarEntry>()

        entries.add(BarEntry(3f, nutrients.protein, "protein"))
        entries.add(BarEntry(2f, nutrients.fat, "fat"))
        entries.add(BarEntry(1f, nutrients.carbs, "carbs"))

        val barDataSet = BarDataSet(entries, "g")
        val data = BarData(barDataSet)
        binding.barChart.data = data // set the data and list of lables into chart
        barDataSet.color = getColor(requireContext(), R.color.purple)

        binding.barChart.animateY(0)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}