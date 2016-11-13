module Metric.View exposing (view)

import Metric.Model exposing (..)
import Metric.Messages exposing (..)
import Html exposing (Html, div, text)
import Html.Attributes exposing (class)


view : Model -> Html Msg
view model =
    div [ class "metric" ]
        [ div [ class "metric__key" ]
            [ text model.key ]
        , div [ class "metric__value" ]
            [ text model.displayValue ]
        ]
