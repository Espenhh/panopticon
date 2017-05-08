module App.Model exposing (Model, Flags, AppState)

import Components.Model
import Detail.Model
import Nav.Model


type alias Flags =
    { url : String
    , token : String
    }


type alias AppState =
    { url : String
    , token : Maybe String
    }


type alias Model =
    { appState : AppState
    , components : Components.Model.Model
    , detail : Detail.Model.Model
    , page : Nav.Model.Page
    }
