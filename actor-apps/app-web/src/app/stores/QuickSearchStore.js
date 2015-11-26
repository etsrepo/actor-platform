/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { forEach } from 'lodash';

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from 'constants/ActorAppConstants';

import DialogStore from 'stores/DialogStore';
import ContactStore from 'stores/ContactStore';

let _isOpen = false,
    _list = [],
    _results = [];

class QuickSearchStore extends Store {
  isOpen() {
    return _isOpen;
  }

  getResults() {
    return _results;
  }

  handleSearchQuery(query) {
    let results = [];

    if (query === '') {
      results = _list;
    } else {
      forEach(_list, (result) => {
        if (result.peerInfo.title.toLowerCase().includes(query.toLowerCase())) {
          results.push(result);
        }
      })
    }

    _results = results;
    this.__emitChange();
  }

  __onDispatch = (action) => {
    switch (action.type) {
      case ActionTypes.QUICK_SEARCH_SHOW:
        _isOpen = true;
        this.handleSearchQuery('');
        this.__emitChange();
        break;

      case ActionTypes.QUICK_SEARCH_HIDE:
        _isOpen = false;
        _results = [];
        this.__emitChange();
        break;

      case ActionTypes.QUICK_SEARCH_CHANGED:
        _list = action.list;
        this.__emitChange();
        break;

      case ActionTypes.QUICK_SEARCH:
        this.handleSearchQuery(action.query);
        break;
    }
  }
}

export default new QuickSearchStore(Dispatcher);