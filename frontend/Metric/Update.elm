module Metric.Update exposing (update)

import Metric.Model exposing (..)
import Metric.Messages exposing (..)


update : Msg -> Model -> Model
update msg model =
    case msg of
        Update ->
            model
