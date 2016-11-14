module Metric.Decoder exposing (decoder)

import Metric.Model exposing (..)
import Json.Decode exposing (Decoder, succeed, int, string, andThen, (:=))
import Json.Decode.Extra exposing ((|:))


decoder : Decoder Model
decoder =
    succeed Model
        |: ("key" := string)
        |: (("status" := string) `andThen` decodeStatus)
        |: ("displayValue" := string)
        |: ("numericValue" := int)


decodeStatus : String -> Decoder Status
decodeStatus status =
    case status of
        "INFO" ->
            succeed Info

        "WARN" ->
            succeed Warn

        _ ->
            succeed Error
