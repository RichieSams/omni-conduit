package richiesams.omniconduit.conduits

import com.google.gson.JsonObject
import richiesams.omniconduit.api.conduits.Conduit
import richiesams.omniconduit.api.conduits.ConduitEntity

class EnergyConduit(jsonObject: JsonObject, factory: Factory<out ConduitEntity>) : Conduit("energy", jsonObject, factory) {
}