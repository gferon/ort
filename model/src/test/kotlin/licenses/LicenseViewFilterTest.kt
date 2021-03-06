/*
 * Copyright (C) 2020 HERE Europe B.V.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.model.licenses

import io.kotest.assertions.show.show
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.containAll
import io.kotest.matchers.neverNullMatcher

import org.ossreviewtoolkit.model.LicenseSource
import org.ossreviewtoolkit.model.Package
import org.ossreviewtoolkit.model.config.CopyrightGarbage
import org.ossreviewtoolkit.model.utils.SimplePackageConfigurationProvider
import org.ossreviewtoolkit.spdx.SpdxExpression
import org.ossreviewtoolkit.spdx.SpdxSingleLicenseExpression
import org.ossreviewtoolkit.spdx.toSpdx
import org.ossreviewtoolkit.utils.storage.FileArchiver

class LicenseViewFilterTest : AbstractLicenseViewTest() {
    private val licenseInfoResolver = LicenseInfoResolver(
        DefaultLicenseInfoProvider(ortResult, SimplePackageConfigurationProvider()),
        CopyrightGarbage(),
        FileArchiver.DEFAULT
    )

    override fun LicenseView.getLicensesWithSources(
        pkg: Package
    ): List<Pair<SpdxSingleLicenseExpression, LicenseSource>> =
        filter(licenseInfoResolver.resolveLicenseInfo(pkg.id)).licenses.flatMap { resolvedLicense ->
            resolvedLicense.sources.map { resolvedLicense.license to it }
        }

    override fun containLicensesWithSources(
        vararg licenses: Pair<String, LicenseSource>
    ): Matcher<List<Pair<SpdxExpression, LicenseSource>>?> =
        neverNullMatcher { value ->
            val expectedLicenses = licenses.map { it.first.toSpdx() }.toSet()
            val actualLicenses = value.map { it.first }.toSet()

            if (expectedLicenses == actualLicenses) {
                containAll(licenses.map { Pair(it.first.toSpdx(), it.second) }).test(value)
            } else {
                MatcherResult(
                    false,
                    "List should contain exactly licenses ${expectedLicenses.show().value}, but has " +
                            actualLicenses.show().value,
                    "List should not contain exactly licenses ${expectedLicenses.show().value}"
                )
            }
        }
}
