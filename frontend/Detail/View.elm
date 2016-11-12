module Detail.View exposing (view)

import Detail.Model exposing (..)
import Detail.Messages exposing (..)
import Html exposing (Html, div, text)
import Html.Attributes exposing (class)


view : Model -> Html Msg
view model =
    div [ class "detail" ] [ text "test" ]
