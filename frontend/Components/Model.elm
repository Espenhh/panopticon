module Components.Model exposing (Model, Environment, init)

import Component.Model


type alias Model =
    { environments : List Environment
    }


type alias Environment =
    { name : String
    , components : List Component.Model.Model
    }


init : Model
init =
    Model []
