module App.View exposing (view)

import Html exposing (Html, div)
import Html.App
import Html.Attributes exposing (class)
import Component.Model
import Component.View
import App.Model exposing (..)
import App.Messages exposing (..)
import List


view : Model -> Html Msg
view model =
    let
        components =
            List.map viewComponent model.components
    in
        div [ class "components" ] components


viewComponent : Component.Model.Model -> Html Msg
viewComponent model =
    Html.App.map ComponentMsg (Component.View.view model)
