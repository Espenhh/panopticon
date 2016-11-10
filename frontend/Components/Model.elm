module Components.Model exposing (Model, init)

import Component.Model


type alias Model =
    { components : List Component.Model.Model
    }


init : Model
init =
    Model []
