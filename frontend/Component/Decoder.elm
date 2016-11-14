module Component.Decoder exposing (decoder)

import Component.Model exposing (..)
import Json.Decode exposing (Decoder, succeed, string, at, andThen, (:=))
import Json.Decode.Extra exposing ((|:))


decoder : Decoder Model
decoder =
    succeed Model
        |: ("environment" := string)
        |: ("system" := string)
        |: ("component" := string)
        |: ("server" := string)
        |: (("overallStatus" := string) `andThen` decodeStatus)
        |: (at [ "links", "details" ] string)


decodeStatus : String -> Decoder Status
decodeStatus status =
    case status of
        "INFO" ->
            succeed Info

        "WARN" ->
            succeed Warn

        _ ->
            succeed Error
