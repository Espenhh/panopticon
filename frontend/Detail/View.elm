module Detail.View exposing (view)

import Detail.Model exposing (..)
import Detail.Messages exposing (..)
import Metric.Model
import Metric.View
import Html exposing (Html, div, text, h1, h2)
import Html.Attributes exposing (class)


view : Model -> Html Msg
view model =
    let
        metrics =
            List.map viewMetric model.measurements
    in
        div [ class "detail" ]
            [ div [ class "detail__header" ]
                [ h1 [ class "detail__component" ] [ text <| model.component ++ " (" ++ model.environment ++ ")" ]
                , h2 [ class "detail__server " ] [ text model.server ]
                ]
            , div [ class "detail__metrics" ] metrics
            ]


viewMetric : Metric.Model.Model -> Html Msg
viewMetric model =
    Html.map (\_ -> Update) <| Metric.View.view model
