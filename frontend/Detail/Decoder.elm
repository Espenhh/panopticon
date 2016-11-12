module Detail.Decoder exposing (decoder)

import Detail.Model exposing (..)
import Json.Decode exposing (Decoder, list, andThen, succeed)
import Metric.Decoder
import Metric.Model


decoder : Decoder Model
decoder =
    list Metric.Decoder.decoder `andThen` decodeModel


decodeModel : List Metric.Model.Model -> Decoder Model
decodeModel metricList =
    succeed (Model metricList)
