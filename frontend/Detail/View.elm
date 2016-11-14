module Detail.View exposing (view)

import Detail.Model exposing (..)
import Detail.Messages exposing (..)
import Metric.Model
import Metric.View
import Html exposing (Html, div, text)
import Html.App
import Html.Attributes exposing (class)


view : Model -> Html Msg
view model =
    let
        metrics =
            List.map viewMetric model.measurements
    in
        div [ class "detail" ]
            [ text model.component
            , div [ class "detail__metrics" ] metrics
            ]


viewMetric : Metric.Model.Model -> Html Msg
viewMetric model =
    Html.App.map (\_ -> Update) <| Metric.View.view model
