module Components.Decoder exposing (decoder)

import Json.Decode exposing (Decoder, list, andThen, succeed)
import Components.Model exposing (..)
import Component.Decoder
import Component.Model


decoder : Decoder Model
decoder =
    list Component.Decoder.decoder `andThen` decodeModel


decodeModel : List Component.Model.Model -> Decoder Model
decodeModel componentList =
    succeed (Model componentList)
