module Metric.View exposing (view)

import Metric.Model exposing (..)
import Metric.Messages exposing (..)
import Html exposing (Html, div, text)
import Html.Attributes exposing (class)


statusIcon : Status -> String
statusIcon status =
    "component__status "
        ++ case status of
            Info ->
                "icon-like"

            Warn ->
                "icon-exclamation"

            Error ->
                "icon-fire"


view : Model -> Html Msg
view model =
    div [ class "metric" ]
        [ div [ class <| statusIcon model.status ] []
        , div [ class "metric__key" ]
            [ text model.key ]
        , div [ class "metric__value" ]
            [ text model.displayValue ]
        ]
