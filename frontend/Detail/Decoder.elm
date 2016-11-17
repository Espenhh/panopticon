module Detail.Decoder exposing (decoder)

import Detail.Model exposing (..)
import Json.Decode exposing (Decoder, succeed, list, string, andThen, succeed, field)
import Json.Decode.Extra exposing ((|:))
import Metric.Decoder


decoder : Decoder Model
decoder =
    succeed Model
        |: (field "environment" string)
        |: (field "system" string)
        |: (field "component" string)
        |: (field "server" string)
        |: (field "measurements" <| list Metric.Decoder.decoder)
