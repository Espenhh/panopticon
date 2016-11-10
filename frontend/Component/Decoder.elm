module Component.Decoder exposing (listDecoder)

import Component.Model exposing (..)
import Json.Decode.Extra exposing ((|:))
import Json.Decode exposing (Decoder, succeed, list, string, (:=))


decoder : Decoder Model
decoder =
    succeed Model
        |: ("id" := string)
        |: ("component" := string)
        |: ("environment" := string)
        |: ("server" := string)
        |: ("system" := string)


listDecoder : Decoder (List Model)
listDecoder =
    list decoder
