package richiesams.omniconduit.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.item.ItemStack


object SerializationUtil {
    val GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .enableComplexMapKeySerialization()
//        .registerTypeAdapter(ItemStack::class.java, ItemStackSerializer())
        .create()
}
