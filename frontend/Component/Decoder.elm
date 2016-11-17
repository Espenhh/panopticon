module Component.Decoder exposing (decoder)

import Component.Model exposing (..)
import Json.Decode exposing (Decoder, succeed, string, at, andThen, field)
import Json.Decode.Extra exposing ((|:))


decoder : Decoder Model
decoder =
    succeed Model
        |: (field "environment" string)
        |: (field "system" string)
        |: (field "component" string)
        |: (field "server" string)
        |: (field "overallStatus" string |> andThen decodeStatus)
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
