module App.View exposing (view)

import Html exposing (Html, div, text)
import Html.Attributes exposing (class)
import Components.View
import Detail.View
import App.Model exposing (..)
import App.Messages exposing (..)
import Nav.Model exposing (Page(..))


view : Model -> Html Msg
view model =
    let
        page =
            viewPage model
    in
        div [ class "app" ]
            [ div [ class "app__sidebar" ] [ text "PANOPTICON" ]
            , div [ class "app__container" ] [ page ]
            ]


viewPage : Model -> Html Msg
viewPage model =
    case model.page of
        Components ->
            componentsView model

        Component _ _ _ _ ->
            detailView model


componentsView : Model -> Html Msg
componentsView model =
    Html.map ComponentsMsg <| Components.View.view model.components


detailView : Model -> Html Msg
detailView model =
    Html.map DetailMsg <| Detail.View.view model.detail
