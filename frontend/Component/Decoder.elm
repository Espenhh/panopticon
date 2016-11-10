module Component.Decoder exposing (listDecoder)

import Component.Model exposing (..)
import Json.Decode exposing (Decoder, succeed, list, string, andThen, (:=))
import Json.Decode.Extra exposing ((|:))


listDecoder : Decoder (List Model)
listDecoder =
    list decoder


decoder : Decoder Model
decoder =
    succeed Model
        |: ("id" := string)
        |: ("component" := string)
        |: ("environment" := string)
        |: ("server" := string)
        |: ("system" := string)
        |: (("status" := string) `andThen` decodeStatus)


decodeStatus : String -> Decoder Status
decodeStatus status =
    case status of
        "INFO" ->
            succeed Info

        "WARN" ->
            succeed Warn

        _ ->
            succeed Error
