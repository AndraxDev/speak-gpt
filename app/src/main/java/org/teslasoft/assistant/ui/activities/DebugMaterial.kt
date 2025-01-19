/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

package org.teslasoft.assistant.ui.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.FullScreenCarouselStrategy
import org.teslasoft.assistant.R
import org.teslasoft.assistant.ui.adapters.MaterialAdapter

class DebugMaterial : FragmentActivity() {
    private var carousel: RecyclerView? = null
    private var carousel2: RecyclerView? = null

    private var sampleData: ArrayList<Int> = arrayListOf()

    private var adapter: MaterialAdapter? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material)

        sampleData.add(ContextCompat.getColor(this, R.color.tint_cat_music))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_cat_development))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_cat_productivity))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_cat_gaming))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_green))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_cat_education))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_cyan))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_blue))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_cat_art))
        sampleData.add(ContextCompat.getColor(this, R.color.tint_cat_business))

        carousel = findViewById(R.id.carousel)
        carousel2 = findViewById(R.id.carousel2)
        carousel?.layoutManager = CarouselLayoutManager()
        carousel2?.layoutManager = CarouselLayoutManager(FullScreenCarouselStrategy())

        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(carousel2)

        adapter = MaterialAdapter(sampleData)
        carousel?.adapter = adapter
        carousel2?.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adjustPaddings()
    }

    private fun adjustPaddings() {
        if (Build.VERSION.SDK_INT < 35) return
        try {
            val root = findViewById<ConstraintLayout>(R.id.root)
            root?.setPadding(
                0,
                window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.statusBars()).top,
                0,
                window.decorView.rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom
            )
        } catch (_: Exception) { /* unused */ }
    }
}
