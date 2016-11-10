module Main exposing (..)

import Navigation
import App.Subscriptions exposing (subscriptions)
import App.Messages exposing (..)
import App.Model exposing (..)
import App.Update exposing (update)
import App.View exposing (view)
import Components.Model
import Nav.Nav exposing (urlUpdate, hashParser)
import Nav.Model exposing (Page)


initModel : Model
initModel =
    Model Components.Model.init Nav.Model.Components


init : Result String Page -> ( Model, Cmd Msg )
init result =
    urlUpdate result initModel


main : Program Never
main =
    Navigation.program (Navigation.makeParser hashParser)
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        , urlUpdate = urlUpdate
        }
