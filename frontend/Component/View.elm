module Component.View exposing (view)

import Component.Model exposing (..)
import Component.Messages exposing (..)
import Html exposing (Html, div, text, a)
import Html.Attributes exposing (class, href)
import Nav.Nav exposing (toHash)
import Nav.Model exposing (Page(..))


statusIcon : Status -> String
statusIcon status =
    "component__status "
        ++ case status of
            Info ->
                ""

            Warn ->
                "icon-exclamation"

            Error ->
                "icon-fire"


url : Model -> String
url model =
    toHash <| Component model.environment model.system model.component model.server


view : Model -> Html Msg
view model =
    a [ href <| url model, class "component" ]
        [ div [ class <| statusIcon model.status ] []
        , div [ class "component__name" ]
            [ text model.component ]
        , div [ class "component__server" ]
            [ text model.server ]
        ]
