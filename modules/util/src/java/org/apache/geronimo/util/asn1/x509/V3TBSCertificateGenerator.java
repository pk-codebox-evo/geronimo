/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.util.asn1.x509;

import org.apache.geronimo.util.asn1.ASN1EncodableVector;
import org.apache.geronimo.util.asn1.DERInteger;
import org.apache.geronimo.util.asn1.DERSequence;
import org.apache.geronimo.util.asn1.DERTaggedObject;
import org.apache.geronimo.util.asn1.DERUTCTime;

/**
 * Generator for Version 3 TBSCertificateStructures.
 * <pre>
 * TBSCertificate ::= SEQUENCE {
 *      version          [ 0 ]  Version DEFAULT v1(0),
 *      serialNumber            CertificateSerialNumber,
 *      signature               AlgorithmIdentifier,
 *      issuer                  Name,
 *      validity                Validity,
 *      subject                 Name,
 *      subjectPublicKeyInfo    SubjectPublicKeyInfo,
 *      issuerUniqueID    [ 1 ] IMPLICIT UniqueIdentifier OPTIONAL,
 *      subjectUniqueID   [ 2 ] IMPLICIT UniqueIdentifier OPTIONAL,
 *      extensions        [ 3 ] Extensions OPTIONAL
 *      }
 * </pre>
 *
 */
public class V3TBSCertificateGenerator
{
    DERTaggedObject         version = new DERTaggedObject(0, new DERInteger(2));

    DERInteger              serialNumber;
    AlgorithmIdentifier     signature;
    X509Name                issuer;
    Time                    startDate, endDate;
    X509Name                subject;
    SubjectPublicKeyInfo    subjectPublicKeyInfo;
    X509Extensions          extensions;

    public V3TBSCertificateGenerator()
    {
    }

    public void setSerialNumber(
        DERInteger  serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    public void setSignature(
        AlgorithmIdentifier    signature)
    {
        this.signature = signature;
    }

    public void setIssuer(
        X509Name    issuer)
    {
        this.issuer = issuer;
    }

    public void setStartDate(
        DERUTCTime startDate)
    {
        this.startDate = new Time(startDate);
    }

    public void setStartDate(
        Time startDate)
    {
        this.startDate = startDate;
    }

    public void setEndDate(
        DERUTCTime endDate)
    {
        this.endDate = new Time(endDate);
    }

    public void setEndDate(
        Time endDate)
    {
        this.endDate = endDate;
    }

    public void setSubject(
        X509Name    subject)
    {
        this.subject = subject;
    }

    public void setSubjectPublicKeyInfo(
        SubjectPublicKeyInfo    pubKeyInfo)
    {
        this.subjectPublicKeyInfo = pubKeyInfo;
    }

    public void setExtensions(
        X509Extensions    extensions)
    {
        this.extensions = extensions;
    }

    public TBSCertificateStructure generateTBSCertificate()
    {
        if ((serialNumber == null) || (signature == null)
            || (issuer == null) || (startDate == null) || (endDate == null)
            || (subject == null) || (subjectPublicKeyInfo == null))
        {
            throw new IllegalStateException("not all mandatory fields set in V3 TBScertificate generator");
        }

        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(version);
        v.add(serialNumber);
        v.add(signature);
        v.add(issuer);

        //
        // before and after dates
        //
        ASN1EncodableVector  validity = new ASN1EncodableVector();

        validity.add(startDate);
        validity.add(endDate);

        v.add(new DERSequence(validity));

        v.add(subject);

        v.add(subjectPublicKeyInfo);

        if (extensions != null)
        {
            v.add(new DERTaggedObject(3, extensions));
        }

        return new TBSCertificateStructure(new DERSequence(v));
    }
}
