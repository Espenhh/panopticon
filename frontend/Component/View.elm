module Component.View exposing (view)

import Component.Model exposing (..)
import Component.Messages exposing (..)
import Html exposing (Html, div, text)
import Html.Attributes exposing (class)


view : Model -> Html Msg
view model =
    div [ class "component" ]
        [ text model.component ]
