module Metric.Decoder exposing (decoder)

import Metric.Model exposing (..)
import Json.Decode exposing (Decoder, succeed, int, string, andThen, field)
import Json.Decode.Extra exposing ((|:))


decoder : Decoder Model
decoder =
    succeed Model
        |: (field "key" string)
        |: (field "status" string |> andThen decodeStatus)
        |: (field "displayValue" string)
        |: (field "numericValue" int)


decodeStatus : String -> Decoder Status
decodeStatus status =
    case status of
        "INFO" ->
            succeed Info

        "WARN" ->
            succeed Warn

        _ ->
            succeed Error
