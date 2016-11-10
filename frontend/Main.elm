module Main exposing (..)

import App.Model exposing (init)
import App.Subscriptions exposing (subscriptions)
import App.Update exposing (update)
import App.View exposing (view)
import Html.App exposing (program, map)


main : Program Never
main =
    program { init = init, update = update, view = view, subscriptions = subscriptions }
