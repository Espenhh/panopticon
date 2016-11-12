module Metric.View exposing (view)

import Metric.Model exposing (..)
import Metric.Messages exposing (..)
import Html exposing (Html, div, text)
import Html.Attributes exposing (class)


view : Model -> Html Msg
view model =
    div [ class "metric" ] [ text model.key ]
