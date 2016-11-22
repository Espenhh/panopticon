module Components.View exposing (view)

import Components.Model exposing (..)
import Components.Messages exposing (..)
import Component.Model
import Component.View
import Html exposing (Html, div, text)
import Html.Attributes exposing (class)
import List
import String exposing (toUpper, left, dropLeft)


view : Model -> Html Msg
view model =
    let
        components =
            List.map viewEnvironment model.environments
    in
        div [ class "components" ] components


viewEnvironment : Components.Model.Environment -> Html Msg
viewEnvironment environment =
    let
        components =
            List.map viewComponent environment.components
    in
        div [ class "components__environment" ]
            [ div [ class "components__environment-name" ] [ text <| capitalize environment.name ]
            , div [ class "components__environment-components" ] components
            ]


viewComponent : Component.Model.Model -> Html Msg
viewComponent model =
    Html.map ComponentMsg (Component.View.view model)


capitalize : String -> String
capitalize s =
    toUpper (left 1 s) ++ dropLeft 1 s
