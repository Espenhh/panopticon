module App.Model exposing (Model)

import Components.Model
import Detail.Model
import Nav.Model


type alias Model =
    { components : Components.Model.Model
    , detail : Detail.Model.Model
    , page : Nav.Model.Page
    }
