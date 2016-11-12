module App.View exposing (view)

import Html exposing (Html, div)
import Html.App
import Html.Attributes exposing (class)
import Components.View
import Detail.View
import App.Model exposing (..)
import App.Messages exposing (..)
import Nav.Model exposing (Page(..))


view : Model -> Html Msg
view model =
    case model.page of
        Components ->
            div [ class "container" ] [ componentsView model ]

        Component index ->
            div [ class "container" ] [ detailView model ]


componentsView : Model -> Html Msg
componentsView model =
    Html.App.map ComponentsMsg <| Components.View.view model.components


detailView : Model -> Html Msg
detailView model =
    Html.App.map DetailMsg <| Detail.View.view model.detail
