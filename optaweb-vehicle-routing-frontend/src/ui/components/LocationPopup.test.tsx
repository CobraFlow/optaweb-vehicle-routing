/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import * as React from 'react';
import { Location } from 'store/route/types';
import LocationPopup, { Props } from './LocationPopup';

const location: Location = {
  id: 1,
  lat: 1.345678,
  lng: 1.345678,
  description: 'location1',
};

describe('Location Popup', () => {
  it('render description', () => {
    const props: Props = {
      removeHandler: jest.fn(),
      updateHandler: jest.fn(),
      cancelHandler: jest.fn(),
      location,
    };
    const locationPopup = shallow(<LocationPopup {...props} />);
    expect(toJson(locationPopup)).toMatchSnapshot();
  });

  it('should call update handler when clicked', () => {
    const props: Props = {
      removeHandler: jest.fn(),
      updateHandler: jest.fn(),
      cancelHandler: jest.fn(),
      location,
    };
    const locationPopup = shallow(<LocationPopup {...props} />);
    locationPopup.find('#save-button').simulate('click');
    expect(props.updateHandler).toBeCalled();
  });

  it('should call remove handler when clicked', () => {
    const props: Props = {
      removeHandler: jest.fn(),
      updateHandler: jest.fn(),
      cancelHandler: jest.fn(),
      location,
    };
    const locationPopup = shallow(<LocationPopup {...props} />);
    locationPopup.find('#remove-button').simulate('click');
    expect(props.removeHandler).toBeCalled();
  });

  it('should call cancel handler when clicked', () => {
    const props: Props = {
      removeHandler: jest.fn(),
      updateHandler: jest.fn(),
      cancelHandler: jest.fn(),
      location,
    };
    const locationPopup = shallow(<LocationPopup {...props} />);
    locationPopup.find('#cancel-button').simulate('click');
    expect(props.cancelHandler).toBeCalled();
  });
});
