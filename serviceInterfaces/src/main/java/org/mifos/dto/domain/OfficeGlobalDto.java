/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.dto.domain;


/**
 * this class is used for holding Office globalNum and displayName(including centers,groups).
 *
 */
public class OfficeGlobalDto {

	private String globalOfficeNum;
	private String displayName;

	public OfficeGlobalDto(){
		this.globalOfficeNum = globalOfficeNum;
		this.displayName = displayName;
	}


	public OfficeGlobalDto(String globalOfficeNum,String displayName){
		super();
		this.globalOfficeNum = globalOfficeNum;
		this.displayName = displayName;
	}


	public String getGlobalOfficeNum() {
		return globalOfficeNum;
	}
	public void setGlobalOfficeNum(String globalOfficeNum) {
		this.globalOfficeNum = globalOfficeNum;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


}