module App.Model exposing (Model, Flags)

import Components.Model
import Detail.Model
import Nav.Model


type alias Flags =
    { url : String
    }


type alias Model =
    { flags : Flags
    , components : Components.Model.Model
    , detail : Detail.Model.Model
    , page : Nav.Model.Page
    }
