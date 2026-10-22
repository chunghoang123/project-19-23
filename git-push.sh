#!/usr/bin/env bash
# =============================================================================
#  Đẩy toàn bộ dự án lên GitHub - tự thử SSH trước, fallback HTTPS.
#
#    ./git-push.sh
#
#  SSH  : cần thêm public key tại https://github.com/settings/ssh/new
#          (key đã lưu ở ~/github_ssh_key.pub)
#  HTTPS: Username = chunghoang123, Password = Personal Access Token
#          token BẮT BUỘC có scope: public_repo (repo này là public)
# =============================================================================
set -uo pipefail
cd "$(dirname "$0")"

REPO="chunghoang123/project-19-23"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  Đẩy dự án CineX lên GitHub                                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo
echo "Nhánh   : $(git branch --show-current)"
echo "Commit  : $(git log --oneline -1)"
echo "Số file : $(git ls-files | wc -l)"
echo

# ------------------------------------------------------------------ SSH
if ssh -o BatchMode=yes -o ConnectTimeout=10 -T git@github.com 2>&1 | grep -qi "successfully authenticated"; then
    echo "→ SSH key hợp lệ, đẩy bằng SSH"
    git remote set-url origin "git@github.com:${REPO}.git"
    if git push -u origin main; then
        echo; echo "✔ Đẩy xong (SSH): https://github.com/${REPO}"; exit 0
    fi
    echo "✗ SSH push lỗi, thử HTTPS"
fi

# ---------------------------------------------------------------- HTTPS
git remote set-url origin "https://github.com/${REPO}.git"
echo "→ HTTPS + Personal Access Token"
echo "    Username : chunghoang123"
echo "    Password : PAT  (token phải có scope 'public_repo')"
echo
echo "  Kiểm tra scope của token trước khi push:"
echo "    curl -sI -H \"Authorization: Bearer <TOKEN>\" https://api.github.com/user | grep -i x-oauth-scopes"
echo "    → phải hiện 'public_repo' hoặc 'repo' (rỗng = chưa tick scope)"
echo
if git push -u origin main; then
    echo; echo "✔ Đẩy xong: https://github.com/${REPO}"
else
    echo; echo "✗ 403 = token chưa có quyền ghi. Sửa scope:"
    echo "   https://github.com/settings/tokens/new  → tick 'public_repo'"
    echo "   hoặc thêm SSH key: https://github.com/settings/ssh/new"
    exit 1
fi
