/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.robot.extractor.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.robot.Constants;
import org.seasar.robot.RobotSystemException;
import org.seasar.robot.extractor.ExtractException;
import org.seasar.robot.extractor.Extractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shinsuke
 *
 */
public class HtmlExtractor implements Extractor {
    private static final Logger logger = LoggerFactory
            .getLogger(HtmlExtractor.class);

    public String encoding = Constants.UTF_8;

    public Pattern metaCharsetPattern = Pattern
            .compile(
                    "<meta.*content\\s*=\\s*['\"].*;\\s*charset=([\\w\\d\\-_]*)['\"]\\s*/?>",
                    Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    public Pattern htmlTagPattern = Pattern.compile("<[^>]+>");

    /* (non-Javadoc)
     * @see org.seasar.robot.extractor.Extractor#getText(java.io.InputStream)
     */
    public String getText(InputStream in) {
        if (in == null) {
            throw new RobotSystemException("The inputstream is null.");
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(in);
            String enc = getEncoding(bis);
            String content = new String(InputStreamUtil.getBytes(bis), enc);
            return htmlTagPattern.matcher(content).replaceAll("");
        } catch (Exception e) {
            throw new ExtractException(e);
        }
    }

    protected String getEncoding(BufferedInputStream bis) {
        int size = 512;
        byte[] b = new byte[512];
        try {
            bis.mark(size);
            bis.read(b);

            String head = new String(b, encoding);
            if (StringUtil.isBlank(head)) {
                return encoding;
            }
            Matcher matcher = metaCharsetPattern.matcher(head);
            if (matcher.find()) {
                String enc = matcher.group(1);
                if (Charset.isSupported(enc)) {
                    return enc;
                }
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Use a default encoding: " + encoding, e);
            }
        } finally {
            try {
                bis.reset();
            } catch (IOException e) {
                throw new ExtractException(e);
            }
        }

        return encoding;
    }
}