module Component.View exposing (view)

import Component.Model exposing (..)
import Component.Messages exposing (..)
import Html exposing (Html, div, text, a)
import Html.Attributes exposing (class, href)
import Nav.Nav exposing (toHash)
import Nav.Model exposing (Page(..))


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


url : Model -> String
url model =
    toHash <| Component model.id


view : Model -> Html Msg
view model =
    a [ href <| url model, class (componentClass model.status) ]
        [ div [ class "component__name" ]
            [ text model.component ]
        , div [ class "component__server" ]
            [ text model.server ]
        ]
