module Components.View exposing (view)

import Components.Model exposing (..)
import Components.Messages exposing (..)
import Component.Model
import Component.View
import Html exposing (Html, div)
import Html.Attributes exposing (class)
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
    Html.map ComponentMsg (Component.View.view model)
