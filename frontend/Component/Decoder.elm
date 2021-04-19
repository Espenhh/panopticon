module Component.Decoder exposing (decoder)

import Component.Model exposing (..)
import Json.Decode exposing (Decoder, andThen, at, field, string, succeed)
import Json.Decode.Extra exposing ((|:), withDefault)


decoder : Decoder Model
decoder =
    succeed Model
        |: field "environment" maybeString
        |: field "system" maybeString
        |: field "component" maybeString
        |: field "server" maybeString
        |: (field "overallStatus" maybeString |> andThen decodeStatus)
        |: at [ "links", "details" ] maybeString


decodeStatus : String -> Decoder Status
decodeStatus status =
    case status of
        "MISSING" ->
            succeed Missing

        "INFO" ->
            succeed Info

        "WARN" ->
            succeed Warn

        _ ->
            succeed Error


maybeString : Decoder String
maybeString =
    withDefault "null" string
