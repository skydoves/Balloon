/*
 * Copyright (C) 2019 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.balloondemo.recycler

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.skydoves.balloondemo.R

object ItemUtils {

  fun getSamples(context: Context): List<SampleItem> {
    val samples = ArrayList<SampleItem>()
    for (i in 0..1) {
      samples.add(SampleItem(drawable(context, R.drawable.sample0),
        "Vincent",
        "It is such a mysterious place, the land of tears."))
      samples.add(SampleItem(drawable(context, R.drawable.sample1),
        "Vermeer",
        "Be clearly aware of the stars and infinity on high."))
      samples.add(SampleItem(drawable(context, R.drawable.sample2),
        "Mia Vance",
        "The most beautiful things in the world cannot be seen or touched."))
      samples.add(SampleItem(drawable(context, R.drawable.sample3),
        "Monet",
        "And now here is my secret, a very simple secret."))
      samples.add(SampleItem(drawable(context, R.drawable.sample4),
        "Picasso",
        "Everything you can imagine is real."))
    }
    return samples
  }

  fun getCustomSamples(context: Context): List<CustomItem> {
    val samples = ArrayList<CustomItem>()
    samples.add(CustomItem(drawable(context, R.drawable.sample0), "Timeline"))
    samples.add(CustomItem(drawable(context, R.drawable.sample1), "Home"))
    samples.add(CustomItem(drawable(context, R.drawable.sample2), "Profile"))
    samples.add(CustomItem(drawable(context, R.drawable.sample3), "Settings"))
    return samples
  }

  private fun drawable(context: Context, @DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(context, id)
  }
}
