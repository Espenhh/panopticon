module Component.View exposing (view)

import Component.Model exposing (..)
import Component.Messages exposing (..)
import Html exposing (Html, div, text)
import Html.Attributes exposing (class)


componentClass : Status -> String
componentClass status =
    "component "
        ++ case status of
            Info ->
                "component--normal"

            Warn ->
                "component--warn"

            Error ->
                "component--error"


view : Model -> Html Msg
view model =
    div [ class (componentClass model.status) ]
        [ div [ class "component__name" ]
            [ text model.component ]
        , div [ class "component__server" ]
            [ text model.server ]
        ]
