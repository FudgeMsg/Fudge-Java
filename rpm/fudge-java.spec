# Copyright 2009 by OpenGamma Inc and other contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

Name:		fudge-java
Version:	0.2
Release:	beta1%{?dist}
Summary:	Fudge message encoding library for Java

Group:		Development/Libraries
License:	http://www.apache.org/licenses/LICENSE-2.0
URL:		http://www.fudgemsg.org/
Source0:	fudge-java.jar
Source1:	fudge-java-javadocs.jar
BuildRoot:	%(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)
BuildArch:	noarch

Requires:	jpackage-utils jakarta-commons-beanutils

%description
Fudge is a hierarchical, typesafe, binary, self-describing message encoding system.

%prep

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p %{buildroot}%{_javadir}
cp -p %{SOURCE0} %{buildroot}%{_javadir}/%{name}-%{version}.jar
cd %{buildroot}%{_javadir}
ln -s %{name}-%{version}.jar %{name}.jar
mkdir -p %{buildroot}%{_javadocdir}/%{name}-%{version}
cd %{buildroot}%{_javadocdir}/%{name}-%{version}
jar xf %{SOURCE1}
cd ..
ln -s %{name}-%{version} %{name}

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(0644,root,root,0755)
%doc ../SOURCES/README.txt ../SOURCES/LICENSE.txt
%doc %{_javadocdir}/%{name}-%{version}/*
%doc %{_javadocdir}/%{name}
%{_javadir}/%{name}-%{version}.jar
%{_javadir}/%{name}.jar

%changelog
* Fri Jan 8 2010 Andrew Griffin <andrew@opengamma.com
- file created
