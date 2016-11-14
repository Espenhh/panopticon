module Detail.Decoder exposing (decoder)

import Detail.Model exposing (..)
import Json.Decode exposing (Decoder, succeed, list, string, andThen, succeed, (:=))
import Json.Decode.Extra exposing ((|:))
import Metric.Decoder


decoder : Decoder Model
decoder =
    succeed Model
        |: ("environment" := string)
        |: ("system" := string)
        |: ("component" := string)
        |: ("server" := string)
        |: ("measurements" := list Metric.Decoder.decoder)
